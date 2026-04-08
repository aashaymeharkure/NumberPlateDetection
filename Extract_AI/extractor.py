import cv2
import pytesseract
import sys
import os
import csv
import shutil
import json
import re
from datetime import datetime
import warnings
warnings.filterwarnings("ignore", category=FutureWarning)
try:
    from google import genai
    HAS_NEW_GENAI = True
except ImportError:
    import google.generativeai as genai
    HAS_NEW_GENAI = False
from PIL import Image

def configure_tesseract():
    env_cmd = os.getenv("TESSERACT_CMD")
    if env_cmd and os.path.exists(env_cmd):
        pytesseract.pytesseract.tesseract_cmd = env_cmd
        return
    path_cmd = shutil.which("tesseract")
    if path_cmd:
        pytesseract.pytesseract.tesseract_cmd = path_cmd
        return
    if os.name == "nt":
        user = os.getenv("USERNAME", "")
        candidates = [
            r"C:\Program Files\Tesseract-OCR\tesseract.exe",
            r"C:\Program Files (x86)\Tesseract-OCR\tesseract.exe",
            fr"C:\Users\{user}\AppData\Local\Programs\Tesseract-OCR\tesseract.exe",
        ]
        for candidate in candidates:
            if os.path.exists(candidate):
                pytesseract.pytesseract.tesseract_cmd = candidate
                return

def get_gemini_api_key():
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        config_path = os.path.join(script_dir, "..", "src", "gemini_config.json")
        with open(config_path, "r") as f:
            return json.load(f).get("api_key")
    except Exception:
        return "AIzaSyAKkbYvAmSuVBxRNKg4UX_12Y7x-EN1kzw"

def format_indian_plate(text):
    text = "".join(filter(str.isalnum, text.upper()))
    if not text: return ""
    if text[0] == 'W': text = 'M' + text[1:]
    def to_digits_strict(s):
        mapping = {'O': '0', 'L': '4', 'I': '1', 'S': '5', 'Z': '2', 'G': '6', 'T': '7', 'B': '8', 'A': '4', 'D': '0'}
        res = "".join(mapping.get(c, c if c.isdigit() else '0') for c in s)
        return res
    def to_chars_strict(s):
        mapping = {'0': 'O', '1': 'I', '5': 'S', '2': 'Z', '8': 'B', '4': 'A'}
        res = "".join(mapping.get(c, c if c.isalpha() else 'X') for c in s)
        return res
    if len(text) == 9:
        if text[2].isdigit() and not text[3].isdigit(): text = text[0:2] + '0' + text[2:]
        else: text = text[0:4] + '0' + text[4:]
    if len(text) >= 7:
        while len(text) < 10: text += '0'
        final_text = text[:10]
        c1 = to_chars_strict(final_text[0:2])
        n1 = to_digits_strict(final_text[2:4])
        c2 = to_chars_strict(final_text[4:6])
        n2 = to_digits_strict(final_text[6:10])
        return f"{c1} {n1} {c2} {n2}"
    return text

def gemini_extract(cv2_img):
    try:
        api_key = get_gemini_api_key()
        if not api_key: return ""
        
        rgb_img = cv2.cvtColor(cv2_img, cv2.COLOR_BGR2RGB)
        pil_img = Image.fromarray(rgb_img)
        prompt = (
            "You are an ANPR expert. Extract the Indian registration number. "
            "Output exactly 10 alphanumeric characters in format: CC NN CC NNNN. "
            "Include spaces only. Return only the plate text."
        )

        if HAS_NEW_GENAI:
            client = genai.Client(api_key=api_key)
            response = client.models.generate_content(
                model='gemini-1.5-flash-latest',
                contents=[prompt, pil_img]
            )
            text = response.text
        else:
            genai.configure(api_key=api_key)
            model = genai.GenerativeModel('gemini-1.5-flash-latest')
            response = model.generate_content([prompt, pil_img])
            text = response.text

        return format_indian_plate(text.strip().upper())
    except Exception as e:
        # print(f"DEBUG: Gemini error: {e}", file=sys.stderr)
        return ""

def preprocess_and_ocr(plate_roi):
    if plate_roi is None or plate_roi.size == 0: return ""
    res = gemini_extract(plate_roi)
    if len(res) >= 5: return res
    h, w = plate_roi.shape[:2]
    target_width = 800
    scale = target_width / float(w)
    img = cv2.resize(plate_roi, (target_width, int(h * scale)), interpolation=cv2.INTER_CUBIC)
    denoised = cv2.fastNlMeansDenoising(img, h=10)
    clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
    contrast = clahe.apply(denoised)
    variants = []
    _, t1 = cv2.threshold(contrast, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    variants.append(t1)
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (2, 2))
    variants.append(cv2.dilate(t1, kernel, iterations=1))
    variants.append(cv2.adaptiveThreshold(contrast, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2))
    best_text = ""
    for v in variants:
        for psm in ["--psm 7", "--psm 6", "--psm 11"]:
            config = f"-c tessedit_char_whitelist=ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 {psm}"
            text = pytesseract.image_to_string(v, config=config)
            cleaned = "".join(text.split()).strip()
            if len(cleaned) >= 4:
                if len(cleaned) > len(best_text): best_text = cleaned
                if 8 <= len(cleaned) <= 10: return cleaned
    return best_text

def extract_plate_number(image_path):
    if not os.path.exists(image_path): return
    try:
        configure_tesseract()
        img = cv2.imread(image_path)
        if img is None: return
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        cp = cv2.data.haarcascades + "haarcascade_russian_plate_number.xml"
        plate_cascade = cv2.CascadeClassifier(cp)
        plates = plate_cascade.detectMultiScale(gray, 1.1, 3, minSize=(50, 15))
        candidates = []
        if len(plates) > 0:
            for (x, y, w, h) in plates:
                mx, my = int(w * 0.08), int(h * 0.08)
                x1, y1 = max(0, x-mx), max(0, y-my)
                w1, h1 = min(gray.shape[1]-x1, w+2*mx), min(gray.shape[0]-y1, h+2*my)
                roi = gray[y1:y1+h1, x1:x1+w1]
                res = preprocess_and_ocr(roi)
                if res: candidates.append(res)
        if not candidates:
            res = preprocess_and_ocr(gray)
            if res: candidates.append(res)
        if candidates: print(format_indian_plate(max(candidates, key=len)))
        else: print("")
    except Exception: pass

def run_webcam_detection():
    configure_tesseract()
    script_dir = os.path.dirname(os.path.abspath(__file__))
    log_dir = os.path.join(script_dir, "..", "fastDatabase")
    if not os.path.exists(log_dir): os.makedirs(log_dir)
    log_path = os.path.join(log_dir, "detections.csv")
    cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
    if not cap.isOpened(): return
    log_file = open(log_path, "a", newline="")
    writer = csv.writer(log_file)
    cp = cv2.data.haarcascades + "haarcascade_russian_plate_number.xml"
    plate_cascade = cv2.CascadeClassifier(cp)
    cv2.namedWindow("ANPR", cv2.WINDOW_NORMAL)
    while True:
        ret, frame = cap.read()
        if not ret: break
        frame_blurred = cv2.GaussianBlur(frame, (7,7), 0)
        gray = cv2.cvtColor(frame_blurred, cv2.COLOR_BGR2GRAY)
        plates = plate_cascade.detectMultiScale(cv2.equalizeHist(gray), 1.1, 5, minSize=(60, 20))
        for (x,y,w,h) in plates: cv2.rectangle(frame, (x,y), (x+w,y+h), (0,255,0), 2)
        cv2.putText(frame, "Q: Quit | S: Save Snapshot", (10, 25), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 0), 2)
        cv2.imshow("ANPR", frame)
        key = cv2.waitKey(1) & 0xFF
        if key == ord("q"): break
        if key == ord("s"):
            ts = datetime.now().strftime('%Y%m%d_%H%M%S')
            snap_name = f"snapshot_{ts}.png"
            snap_path = os.path.join(script_dir, "..", "Img", snap_name).replace("\\", "/")
            any_detected = False
            for (x,y,w,h) in plates:
                mx, my = int(w*0.05), int(h*0.05)
                roi = gray[max(0,y-my):min(gray.shape[0],y+h+my), max(0,x-mx):min(gray.shape[1],x+w+mx)]
                cleaned = format_indian_plate(preprocess_and_ocr(cv2.bilateralFilter(roi, 11, 17, 17)))
                if len(cleaned) >= 5:
                    writer.writerow([cleaned, ts, snap_path])
                    log_file.flush()
                    print(f"DETECTED: {cleaned} at {ts} image: {snap_path}")
                    any_detected = True
            if True: # Always save if 'S' pressed
                p = max(plates, key=lambda b: b[2]*b[3]) if len(plates) > 0 else (0,0,frame.shape[1],frame.shape[0])
                mx, my = int(p[2]*0.2), int(p[3]*0.2)
                f_crop = frame[max(0,p[1]-my):min(frame.shape[0],p[1]+p[3]+my), max(0,p[0]-mx):min(frame.shape[1],p[0]+p[2]+mx)]
                cv2.imwrite(snap_path, f_crop)
                print(f"Snapshot saved: {snap_path}")
    cap.release()
    log_file.close()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--webcam": run_webcam_detection()
    elif len(sys.argv) > 1: extract_plate_number(sys.argv[1])

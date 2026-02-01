#!/usr/bin/env python3
"""
Nightpass Survivor Test Runner (V4 Compatible - _output suffix fix)
"""

import os
import sys
import subprocess
import time
import argparse
import glob
from pathlib import Path

# Configuration
SRC_DIR = "src"
OUTPUT_DIR = "output"
TEST_BASE_DIR = "testcases"
MAIN_CLASS = "Main"

class Colors:
    def __init__(self):
        if os.name == 'nt':
            # Windows'ta renkleri kapat (varsayılan)
            self.NC = ''
            self.RED = ''
            self.GREEN = ''
            self.YELLOW = ''
            self.BLUE = ''
            self.MAGENTA = ''
            self.CYAN = ''
            self.BOLD = ''
            try:
                import colorama
                colorama.init()
                self.enabled = True
            except:
                self.enabled = False
        else:
            # Unix-like sistemlerde ANSI renkleri
            self.NC = '\033[0m'
            self.RED = '\033[0;31m'
            self.GREEN = '\033[0;32m'
            self.YELLOW = '\033[0;33m'
            self.BLUE = '\033[0;34m'
            self.MAGENTA = '\033[0;35m'
            self.CYAN = '\033[0;36m'
            self.BOLD = '\033[1m'
            self.enabled = True

colors = Colors()

def log_info(msg):
    print(f"{colors.BLUE}[INFO]{colors.NC} {msg}")

def log_success(msg):
    print(f"{colors.GREEN}[ OK ]{colors.NC} {msg}")

def log_warning(msg):
    print(f"{colors.YELLOW}[WARN]{colors.NC} {msg}")

def log_error(msg):
    print(f"{colors.RED}[FAIL]{colors.NC} {msg}")

def ensure_directory(path):
    Path(path).mkdir(parents=True, exist_ok=True)

def compile_java():
    """src içindeki tüm .java dosyalarını derler"""
    log_info("Compiling Java sources...")
    java_files = list(Path(SRC_DIR).glob("*.java"))
    
    if not java_files:
        log_error(f"No .java files found in {SRC_DIR}/")
        return False
    
    cmd = ["javac"] + [str(f.name) for f in java_files]
    try:
        result = subprocess.run(cmd, cwd=SRC_DIR, capture_output=True, text=True)
        if result.returncode != 0:
            log_error("✗ Compilation failed")
            print(result.stderr)
            return False
        log_success("✓ Compilation succeeded")
        return True
    except FileNotFoundError:
        log_error("javac not found. Make sure JDK is installed and on PATH.")
        return False

def get_test_files(test_type=None):
    """inputs klasöründeki dosyaları bulur (type prefix ile filtreler)"""
    if not os.path.exists(TEST_BASE_DIR):
        log_error(f"✗ Test directory '{TEST_BASE_DIR}' not found")
        return []
    
    inputs_dir = Path(TEST_BASE_DIR) / "inputs"
    if not inputs_dir.exists():
        log_error(f"✗ Inputs directory '{inputs_dir}' not found")
        return []
    
    pattern = str(inputs_dir / "*.txt")
    files = sorted(glob.glob(pattern))
    
    # test_type belirtilmişse (ör. type1, type2...) sadece o prefix ile başlayanları al
    if test_type:
        prefix = f"{test_type}_"
        files = [f for f in files if Path(f).name.startswith(prefix)]
    
    return files


def run_tests(test_type=None, verbose=False, benchmark=False):
    input_files = get_test_files(test_type)
    
    if not input_files:
        log_warning(f"⚠ No test files found in {TEST_BASE_DIR}/inputs/")
        return {'total': 0, 'failed': 0}
        
    log_info(f"{'Benchmarking' if benchmark else 'Testing'} {len(input_files)} files...")
    print("-" * 60)
    ensure_directory(OUTPUT_DIR)
    
    stats = {'total': len(input_files), 'passed': 0, 'failed': 0, 'skipped': 0}
    
    for i, f in enumerate(input_files, 1):
        res = run_single_test(f, verbose, benchmark)
        
        status_symbol = {
            'pass': f"{colors.GREEN}✓{colors.NC}",
            'fail': f"{colors.RED}✗{colors.NC}",
            'skip': f"{colors.YELLOW}-{colors.NC}"
        }.get(res['status'], '?')
        
        # Tek satırlık özet log
        print(f"[{i:02d}/{len(input_files):02d}] {status_symbol} {res['name']} "
              f"({res['duration']:.3f}s) - {res['status']}")
        
        # Hata veya skip durumunda detaylı mesaj
        # Fail veya skip durumunda detay yazma
        if res['status'] != 'pass':
            if res['status'] == 'skip':
                print(f"   {colors.YELLOW}Skipped{colors.NC}")
            else:
                print(f"   {colors.RED}Fail{colors.NC}")

        if res['status'] == 'pass': stats['passed'] += 1
        elif res['status'] == 'skip': stats['skipped'] += 1
        else: stats['failed'] += 1

    print("-" * 60)
    print(f"Total: {stats['total']} | Passed: {colors.GREEN}{stats['passed']}{colors.NC} | "
          f"Failed: {colors.RED}{stats['failed']}{colors.NC} | Skipped: {colors.YELLOW}{stats['skipped']}{colors.NC}")
    
    return stats

def run_single_test(input_file_path, verbose=False, benchmark=False):
    """Tek bir testi çalıştırır"""
    
    input_path = Path(input_file_path)
    basename = input_path.stem # örn: type1_1000
    
    # Beklenen output: inputs → outputs, name + "_out"
    try:
        parts = list(input_path.parts)
        if 'inputs' in parts:
            # Klasörü değiştir
            idx = len(parts) - 1 - parts[::-1].index('inputs')
            parts[idx] = 'outputs'
            
            # Dosya ismi: xxx.txt -> xxx_out.txt
            old_filename = parts[-1]
            name_part = Path(old_filename).stem
            suffix_part = Path(old_filename).suffix
            new_filename = f"{name_part}_out{suffix_part}"
            parts[-1] = new_filename
            
            expected_path = Path(*parts)
        else:
            expected_path = Path(str(input_path).replace("inputs", "outputs").replace(".txt", "_out.txt"))
    except Exception:
        expected_path = Path(str(input_path).replace("inputs", "outputs").replace(".txt", "_out.txt"))

    # Bizim ürettiğimiz çıktı dosyası
    actual_path = Path(OUTPUT_DIR) / f"{basename}.txt"
    
    result = {
        'name': basename,
        'duration': 0,
        'status': 'unknown',
        'error_message': ''
    }
    
    # Expected file kontrolü
    if not benchmark and not expected_path.exists():
        result['status'] = 'skip'
        result['error_message'] = f"Missing expected file at:\n   -> {expected_path}"
        return result
    
    try:
        java_input = os.path.relpath(input_path, SRC_DIR)
        java_output = os.path.relpath(actual_path, SRC_DIR)
        
        cmd = ["java", MAIN_CLASS, java_input, java_output]
        
        start_time = time.time()
        process = subprocess.run(cmd, cwd=SRC_DIR, capture_output=True, text=True)
        end_time = time.time()
        
        result['duration'] = end_time - start_time
        
        if process.returncode != 0:
            result['status'] = 'fail'
            result['error_message'] = (
                f"Runtime error (exit code {process.returncode}).\n"
                f"--- STDOUT ---\n{process.stdout}\n"
                f"--- STDERR ---\n{process.stderr}"
            )
            return result
        
        if benchmark:
            # Sadece çalışma süresini raporla
            result['status'] = 'pass'
            return result
        
        # Çıktı dosyalarını kıyasla
        is_equal, diff_msg = compare_files(expected_path, actual_path)
        if is_equal:
            result['status'] = 'pass'
        else:
            result['status'] = 'wrong_output'
            result['error_message'] = diff_msg
        
        return result
        
    except Exception as e:
        result['status'] = 'fail'
        result['error_message'] = f"Exception while running test: {e}"
        return result

def normalize_line(line):
    """Satır sonu, boşluk vs. normalize et"""
    return line.rstrip('\n').rstrip('\r')

def compare_files(expected_path, actual_path, max_diff_lines=10):
    """
    İki dosyanın satır satır aynı olup olmadığını kontrol eder.
    max_diff_lines ile gösterilecek maksimum farklı satır sayısı sınırlandırılır.
    """
    if not actual_path.exists():
        return False, f"Actual output file not found:\n   -> {actual_path}"

    with expected_path.open('r', encoding='utf-8') as f_exp, \
         actual_path.open('r', encoding='utf-8') as f_act:
        exp_lines = f_exp.readlines()
        act_lines = f_act.readlines()

    # Normalize
    exp_lines = [normalize_line(l) for l in exp_lines]
    act_lines = [normalize_line(l) for l in act_lines]

    if exp_lines == act_lines:
        return True, ""

    diff_messages = []
    max_len = max(len(exp_lines), len(act_lines))
    diff_count = 0

    for i in range(max_len):
        exp_line = exp_lines[i] if i < len(exp_lines) else "<no line>"
        act_line = act_lines[i] if i < len(act_lines) else "<no line>"

        if exp_line != act_line:
            diff_count += 1
            if diff_count <= max_diff_lines:
                diff_messages.append(
                    f"Line {i+1}:\n"
                    f"  Expected: {repr(exp_line)}\n"
                    f"  Actual  : {repr(act_line)}\n"
                )
            else:
                break

    if diff_count > max_diff_lines:
        diff_messages.append(f"... and {diff_count - max_diff_lines} more differing lines")

    return False, "\n".join(diff_messages)

def parse_args():
    parser = argparse.ArgumentParser(description="Nightpass Survivor Test Runner")
    parser.add_argument(
        "--type", choices=["type1", "type2", "type3"],
        help="Only run specified type (type1 / type2 / type3)"
    )
    parser.add_argument(
        "--verbose", action="store_true",
        help="Show detailed diff for wrong outputs"
    )
    parser.add_argument(
        "--benchmark", action="store_true",
        help="Only measure execution time, do not compare outputs"
    )
    return parser.parse_args()

def main():
    args = parse_args()
    
    if not os.path.isdir(SRC_DIR):
        log_error(f"Source directory '{SRC_DIR}' not found")
        sys.exit(1)
    
    clean_outputs()
    if not compile_java(): sys.exit(1)
    
    stats = run_tests(args.type, args.verbose, args.benchmark)
    sys.exit(1 if stats['failed'] > 0 else 0)

def clean_outputs():
    try:
        for f in glob.glob(os.path.join(SRC_DIR, "*.class")): os.remove(f)
        for f in glob.glob(os.path.join(OUTPUT_DIR, "*.txt")): os.remove(f)
    except:
        pass

if __name__ == '__main__':
    main()

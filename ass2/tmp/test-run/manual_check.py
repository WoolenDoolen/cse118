from pathlib import Path
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ADB = r'C:\Users\joust\AppData\Local\Android\Sdk\platform-tools\adb.exe'
PACKAGE = 'edu.ucsc.cse118.assignment2'
OUT = Path(__file__).resolve().parent
results = []

def adb(*args):
    return subprocess.run([ADB, '-s', 'emulator-5554', *args], check=True,
                          capture_output=True).stdout

def ui():
    adb('shell', 'uiautomator', 'dump', '/sdcard/assignment2-ui.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/assignment2-ui.xml'))

def find(root, text=None, description=None):
    for node in root.iter('node'):
        if node.get('package') != PACKAGE:
            continue
        if text is not None and node.get('text') == text:
            return node
        if description is not None and node.get('content-desc') == description:
            return node
    raise AssertionError(f'View not found: {text or description}')

def expect(text):
    find(ui(), text=text)

def tap(text=None, description=None):
    node = find(ui(), text, description)
    left, top, right, bottom = map(int, re.findall(r'\d+', node.get('bounds')))
    adb('shell', 'input', 'tap', str((left + right) // 2), str((top + bottom) // 2))
    time.sleep(0.4)

def capture(name):
    (OUT / f'{name}.png').write_bytes(adb('exec-out', 'screencap', '-p'))

def passed(name):
    results.append({'check': name, 'passed': True})
    print('PASS: ' + name, flush=True)

adb('shell', 'am', 'force-stop', PACKAGE)
adb('shell', 'am', 'start', '-n', PACKAGE + '/.MainActivity')
time.sleep(1)
expect('CSE118 Assignment 2')
expect('Login')
assert not any(n.get('package') == PACKAGE and n.get('content-desc') == 'Back'
               for n in ui().iter('node'))
capture('login')
tap('Login')
expect('Workspaces')
capture('workspaces')

tap('No Channels')
expect('No Channels')
root = ui()
assert not any(n.get('resource-id') == PACKAGE + ':id/name' for n in root.iter('node'))
tap(description='Back')
expect('Workspaces')
passed('Workspace with zero channels opens an empty list and returns correctly')

tap('No Messages')
expect('0 Messages')
tap('Channel 1')
expect('Channel 1')
root = ui()
assert not any(n.get('resource-id') == PACKAGE + ':id/name' for n in root.iter('node'))
tap(description='Back')
expect('No Messages')
tap(description='Back')
expect('Workspaces')
passed('Channel with zero messages opens an empty list and returns correctly')

tap('CSE118 Fall 26')
expect('24 Messages')
capture('channels')
tap('Assignment 2')
expect('Max Rist')
capture('messages')
tap('Max Rist')
expect('Aenean sit amet justo. Morbi ut odio.')
expect('Sep 11, 2022, 12:15:43 PM')
capture('detail')
tap(description='Back')
expect('Assignment 2')
tap(description='Back')
expect('CSE118 Fall 26')
tap(description='Back')
expect('Workspaces')
tap(description='Back')
expect('Login')
passed('Toolbar back navigation returns through detail, messages, channels, workspaces, login')

tap('Login')
tap('CSE118 Fall 26')
tap('Assignment 2')
tap('Max Rist')
adb('shell', 'input', 'keyevent', '4')
expect('Assignment 2')
passed('Android system Back returns from detail to its channel')

tap('Max Rist')
old_auto = adb('shell', 'settings', 'get', 'system', 'accelerometer_rotation').decode().strip()
old_rotation = adb('shell', 'settings', 'get', 'system', 'user_rotation').decode().strip()
try:
    adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
    adb('shell', 'settings', 'put', 'system', 'user_rotation', '1')
    time.sleep(1)
    expect('Aenean sit amet justo. Morbi ut odio.')
    expect('Sep 11, 2022, 12:15:43 PM')
    tap(description='Back')
    expect('Assignment 2')
    passed('Message selection and navigation survive screen rotation')
finally:
    for key, value in [('accelerometer_rotation', old_auto), ('user_rotation', old_rotation)]:
        if value == 'null':
            adb('shell', 'settings', 'delete', 'system', key)
        else:
            adb('shell', 'settings', 'put', 'system', key, value)

(OUT / 'manual-results.json').write_text(json.dumps(results, indent=2))
print(f'{len(results)} additional runtime checks passed.', flush=True)

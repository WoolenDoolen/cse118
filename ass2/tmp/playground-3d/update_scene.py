from pathlib import Path
import math
import re
import shutil

project = Path(r'C:\Users\joust\OneDrive\Desktop\Code\cmpm125\ass2')
scene = project / 'Assets/Scenes/PhysicsPlayground.unity'
backup = Path(__file__).resolve().parent / 'original/Assets/Scenes/PhysicsPlayground.unity'
backup.parent.mkdir(parents=True, exist_ok=True)
shutil.copy2(scene, backup)
data = scene.read_text()
if '5c9786b4a6bc4d0ca66cf6ad0831ab27' in data:
    print('Unity already upgraded the scene; keeping the editor-generated scene.')
    raise SystemExit(0)
assert '&968113140' not in data
blocks = re.split(r'(?=^--- !u!)', data, flags=re.M)
pitch = math.atan2(18, 25)
rotation = f'{{x: {math.sin(pitch / 2):.9f}, y: 0, z: 0, w: {math.cos(pitch / 2):.9f}}}'

def replace(block, old, new):
    assert old in block, old
    return block.replace(old, new, 1)

label_objects = set()
for block in blocks:
    if block.startswith('--- !u!102'):
        label_objects.add(re.search(r'm_GameObject: \{fileID: (\d+)\}', block).group(1))
for index, block in enumerate(blocks):
    if block.startswith('--- !u!1 &968113136\n'):
        block = replace(block, '  - component: {fileID: 968113137}',
                        '  - component: {fileID: 968113137}\n  - component: {fileID: 968113140}')
    elif block.startswith('--- !u!4 &968113139\n'):
        block = re.sub(r'm_LocalRotation: \{[^\n]+\}', 'm_LocalRotation: ' + rotation, block)
        block = replace(block, 'm_LocalPosition: {x: 0, y: 24, z: -24}',
                        'm_LocalPosition: {x: 0, y: 18, z: -24}')
    elif block.startswith('--- !u!20 &968113138\n'):
        block = replace(block, 'orthographic: 1', 'orthographic: 0')
        block = replace(block, 'field of view: 60', 'field of view: 51')
        block = replace(block, 'near clip plane: 0.3', 'near clip plane: 0.1')
        block = replace(block, 'far clip plane: 1000', 'far clip plane: 100')
        block = replace(block, '    height: 1', '    height: 0.875')
    elif block.startswith('--- !u!4 &45467271\n'):
        block = replace(block, 'm_LocalPosition: {x: 0, y: 2.5, z: 7}',
                        'm_LocalPosition: {x: 0, y: 1.5, z: 7}')
        block = replace(block, 'm_LocalScale: {x: 4, y: 1, z: 4}',
                        'm_LocalScale: {x: 4, y: 3, z: 4}')
    elif block.startswith('--- !u!108 &1968260530\n'):
        block = replace(block, '    m_Type: 0', '    m_Type: 2')
        block = replace(block, '    m_Strength: 1', '    m_Strength: 0.85')
        block = replace(block, '    m_Bias: 0.05', '    m_Bias: 0.03')
        block = replace(block, '    m_NormalBias: 0.4', '    m_NormalBias: 0.15')
        block = replace(block, '  m_Intensity: 1.2', '  m_Intensity: 1.15')
    elif block.startswith('--- !u!104'):
        block = replace(block, 'm_AmbientMode: 0', 'm_AmbientMode: 3')
        block = replace(block, 'm_AmbientSkyColor: {r: 0.55, g: 0.55, b: 0.6, a: 1}',
                        'm_AmbientSkyColor: {r: 0.3, g: 0.32, b: 0.36, a: 1}')
    elif block.startswith('--- !u!102'):
        block = replace(block, 'm_FontSize: 48', 'm_FontSize: 64')
        block = replace(block, 'm_CharacterSize: 0.09', 'm_CharacterSize: 0.13')
    elif block.startswith('--- !u!4 &'):
        object_match = re.search(r'm_GameObject: \{fileID: (\d+)\}', block)
        if object_match and object_match.group(1) in label_objects:
            block = re.sub(r'm_LocalRotation: \{[^\n]+\}', 'm_LocalRotation: ' + rotation, block)
    blocks[index] = block

component = '''--- !u!114 &968113140
MonoBehaviour:
  m_ObjectHideFlags: 0
  m_CorrespondingSourceObject: {fileID: 0}
  m_PrefabInstance: {fileID: 0}
  m_PrefabAsset: {fileID: 0}
  m_GameObject: {fileID: 968113136}
  m_Enabled: 1
  m_EditorHideFlags: 0
  m_Script: {fileID: 11500000, guid: 5c9786b4a6bc4d0ca66cf6ad0831ab27, type: 3}
  m_Name:
  m_EditorClassIdentifier:
'''
scene.write_text(''.join(blocks) + component, newline='\r\n')
print('Updated the saved scene: perspective camera, shadows, solid platform, larger world labels.')

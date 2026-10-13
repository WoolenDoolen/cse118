from pathlib import Path
import shutil

project = Path(r'C:\Users\joust\OneDrive\Desktop\Code\cmpm125\ass2')
stage = Path(__file__).resolve().parent
backup = stage / 'before-hard-mode'
paths = ['Assets/Scripts/PlaygroundCamera.cs', 'Assets/Scripts/GameManager.cs', 'README.md']
for relative in paths:
    saved = backup / relative
    saved.parent.mkdir(parents=True, exist_ok=True)
    if not saved.exists():
        shutil.copy2(project / relative, saved)

path = project / paths[0]
source = path.read_text(encoding='utf-8')
source = source.replace('    float pitch;\n', '    float pitch;\n    float roll;\n    public bool HardMode { get; private set; }\n')
marker = '        float horizontal = (Input.GetKey(KeyCode.RightArrow) ? 1f : 0f)'
assert marker in source
source = source.replace(marker, '''        if (Input.GetKeyDown(KeyCode.H))
            SetHardMode(!HardMode);
        if (HardMode)
        {
            AdvanceHardMode(Time.deltaTime);
            return;
        }

''' + marker)
source = source.replace('''        transform.rotation = Quaternion.Euler(pitch, yaw, 0f);
        transform.position = focus - transform.forward * new Vector3(0f, 18f, -25f).magnitude;
    }

    public void ResetView()''', '''        ApplyOrbitPose();
    }

    public void SetHardMode(bool enabled)
    {
        if (enabled)
            HardMode = true;
        else
            ResetView();
    }

    public void AdvanceHardMode(float deltaTime)
    {
        if (!HardMode)
            return;

        pitch = Mathf.Repeat(pitch + 17f * deltaTime, 360f);
        yaw = Mathf.Repeat(yaw + 24f * deltaTime, 360f);
        roll = Mathf.Repeat(roll + 31f * deltaTime, 360f);
        ApplyOrbitPose();
    }

    void ApplyOrbitPose()
    {
        transform.rotation = Quaternion.Euler(pitch, yaw, roll);
        transform.position = focus - transform.forward * new Vector3(0f, 18f, -25f).magnitude;
    }

    public void ResetView()''')
source = source.replace('''    public void ResetView()
    {
        yaw = 0f;''', '''    public void ResetView()
    {
        HardMode = false;
        roll = 0f;
        yaw = 0f;''')
path.write_text(source, encoding='utf-8')

path = project / paths[1]
source = path.read_text(encoding='utf-8').replace('    GUIStyle titleStyle;\n', '    GUIStyle titleStyle;\n    PlaygroundCamera playgroundCamera;\n')
source = source.replace('''        float width = Mathf.Max(1f, Screen.width - 24f);''', '''        if (playgroundCamera == null && Camera.main != null)
            playgroundCamera = Camera.main.GetComponent<PlaygroundCamera>();
        bool hardMode = playgroundCamera != null && playgroundCamera.HardMode;
        float width = Mathf.Max(1f, Screen.width - 24f);''')
source = source.replace('''            "PHYSICS PLAYGROUND    Gold cubes: " + collected + " / " + totalPickups, titleStyle);''',
'''            (hardMode ? "HARD MODE    Gold cubes: " : "PHYSICS PLAYGROUND    Gold cubes: ")
            + collected + " / " + totalPickups, titleStyle);''')
source = source.replace('WASD: move    Space: jump    E: spawn at blue button    R: restart    Arrows: camera    L: reset camera',
                        'WASD: move    Space: jump    E: spawn    R: restart    Arrows: camera    L: reset    H: hard mode')
path.write_text(source, encoding='utf-8')

path = project / paths[2]
source = path.read_text(encoding='utf-8').replace(
    'the viewing angle, and **L** to reset the camera to its starting position.',
    'the viewing angle, and **L** to reset the camera to its starting position.\n'
    'Press **H** to toggle **hard mode**: the camera continuously rotates on all three\n'
    'axes, including full flips and rolls. Arrow camera controls pause during hard mode.\n'
    'Press **H** again or **L** to turn hard mode off and restore the normal view.')
path.write_text(source, encoding='utf-8')

for relative in paths[:2]:
    shutil.copy2(project / relative, stage / 'verify-project' / relative)
print('Added H hard mode, continuous three-axis rotation, HUD status, and reset behavior.')

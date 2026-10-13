from pathlib import Path
import shutil

project = Path(r'C:\Users\joust\OneDrive\Desktop\Code\cmpm125\ass2')
stage = Path(__file__).resolve().parent
backup = stage / 'before-camera-controls'
paths = ['Assets/Scripts/PlaygroundCamera.cs', 'Assets/Scripts/PlayerController.cs',
         'Assets/Scripts/GameManager.cs', 'README.md']
for relative in paths:
    saved = backup / relative
    saved.parent.mkdir(parents=True, exist_ok=True)
    if not saved.exists():
        shutil.copy2(project / relative, saved)

path = project / paths[0]
source = path.read_text(encoding='utf-8')
source = source.replace('    Camera view;\n', '''    Camera view;
    TextMesh[] labels;
    float yaw;
    float pitch;
    static readonly Vector3 focus = new Vector3(0f, 0f, 1f);
    const float OrbitSpeed = 45f;
''')
source = source.replace('''        transform.position = new Vector3(0f, 18f, -24f);
        transform.LookAt(new Vector3(0f, 0f, 1f));
        UpdateFraming();
''', '''        labels = FindObjectsByType<TextMesh>(FindObjectsSortMode.None);
        ResetView();
''')
source = source.replace('''    void LateUpdate()
    {
        UpdateFraming();
    }
''', '''    void Update()
    {
        if (Input.GetKeyDown(KeyCode.L))
        {
            ResetView();
            return;
        }

        float horizontal = (Input.GetKey(KeyCode.RightArrow) ? 1f : 0f)
                         - (Input.GetKey(KeyCode.LeftArrow) ? 1f : 0f);
        float vertical = (Input.GetKey(KeyCode.UpArrow) ? 1f : 0f)
                       - (Input.GetKey(KeyCode.DownArrow) ? 1f : 0f);
        Orbit(new Vector2(horizontal, vertical), Time.deltaTime);
    }

    public void Orbit(Vector2 direction, float deltaTime)
    {
        direction = Vector2.ClampMagnitude(direction, 1f);
        yaw = Mathf.Repeat(yaw + direction.x * OrbitSpeed * deltaTime, 360f);
        pitch = Mathf.Clamp(pitch + direction.y * OrbitSpeed * deltaTime, 20f, 80f);
        transform.rotation = Quaternion.Euler(pitch, yaw, 0f);
        transform.position = focus - transform.forward * new Vector3(0f, 18f, -25f).magnitude;
    }

    public void ResetView()
    {
        yaw = 0f;
        pitch = Mathf.Atan2(18f, 25f) * Mathf.Rad2Deg;
        transform.position = new Vector3(0f, 18f, -24f);
        transform.LookAt(focus);
        UpdateFraming();
        FaceLabels();
    }

    void FaceLabels()
    {
        if (labels == null)
            return;
        foreach (TextMesh label in labels)
            if (label != null)
                label.transform.rotation = transform.rotation;
    }

    void LateUpdate()
    {
        UpdateFraming();
        FaceLabels();
    }
''')
source = source.replace('new Vector3(x * 12.6f, y, z * 12.6f)', 'new Vector3(x * 12.6f, y < 0 ? -1f : 8f, z * 12.6f)')
source = source.replace('// Widen the view on narrow windows while keeping the same camera pose.',
                        '// Fit the arena and jump height from the current camera angle.')
path.write_text(source, encoding='utf-8')

path = project / paths[1]
source = path.read_text(encoding='utf-8')
for key in ['D', 'A', 'W', 'S']:
    arrow = {'D': 'RightArrow', 'A': 'LeftArrow', 'W': 'UpArrow', 'S': 'DownArrow'}[key]
    source = source.replace(f'Input.GetKey(KeyCode.{key}) || Input.GetKey(KeyCode.{arrow})',
                            f'Input.GetKey(KeyCode.{key})')
path.write_text(source, encoding='utf-8')

path = project / paths[2]
source = path.read_text(encoding='utf-8').replace(
    'WASD / arrows: move    Space: jump    E: spawn at blue button    R: restart',
    'WASD: move    Space: jump    E: spawn at blue button    R: restart    Arrows: camera    L: reset camera')
path.write_text(source, encoding='utf-8')

path = project / paths[3]
source = path.read_text(encoding='utf-8').replace('The fixed perspective camera,', 'The perspective camera,')
source = source.replace('Move with **WASD / arrow keys**, jump with **Space**, and restart with **R**.',
    'Move with **WASD**, jump with **Space**, and restart with **R**.\n'
    'Use **left/right arrows** to orbit the camera, **up/down arrows** to raise or lower\n'
    'the viewing angle, and **L** to reset the camera to its starting position.')
path.write_text(source, encoding='utf-8')

for relative in paths[:3]:
    shutil.copy2(project / relative, stage / 'verify-project' / relative)
print('Camera controls added to the Unity project and isolated verification copy.')

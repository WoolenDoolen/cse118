from pathlib import Path
import shutil

stage = Path(__file__).resolve().parent
project = Path(r'C:\Users\joust\OneDrive\Desktop\Code\cmpm125\ass2')
path = project / 'Assets/Scripts/PlaygroundCamera.cs'
source = path.read_text(encoding='utf-8').replace(
    'new Vector3(x * 12.6f, y < 0 ? -1f : 8f, z * 12.6f)',
    'new Vector3(x * 12.6f, y, z * 12.6f)')
source = source.replace('        view.fieldOfView = Mathf.Clamp(', '''        Vector3 jumpTop = transform.InverseTransformPoint(new Vector3(0f, 8f, 7f));
        if (jumpTop.z > 0f)
            tangent = Mathf.Max(tangent, Mathf.Abs(jumpTop.y) / jumpTop.z * 1.08f,
                Mathf.Abs(jumpTop.x) / (jumpTop.z * aspect) * 1.08f);
        view.fieldOfView = Mathf.Clamp(''')
path.write_text(source, encoding='utf-8')
shutil.copy2(path, stage / 'verify-project/Assets/Scripts/PlaygroundCamera.cs')

path = stage / 'verify-project/Assets/Scripts/PlaygroundVerification.cs'
source = path.read_text(encoding='utf-8')
marker = '        yield return new WaitForEndOfFrame();\n        Capture(Path.Combine(output, "perspective-view.png"));'
assert marker in source
source = source.replace(marker, '''        PlaygroundCamera controls = camera.GetComponent<PlaygroundCamera>();
        Vector3 initialPosition = camera.transform.position;
        Quaternion initialRotation = camera.transform.rotation;
        float initialFov = camera.fieldOfView;
        controls.Orbit(Vector2.right, 1f);
        Check(Vector3.Distance(initialPosition, camera.transform.position) > 10f,
            "Horizontal camera controls orbit around the arena");
        float oldHeight = camera.transform.position.y;
        controls.Orbit(Vector2.up, 0.5f);
        Check(camera.transform.position.y > oldHeight + 1f,
            "Vertical camera controls raise the viewing angle");
        yield return new WaitForEndOfFrame();
        Capture(Path.Combine(output, "orbit-view.png"));
        bool framed = true;
        foreach (Vector2 direction in new[] { Vector2.down, Vector2.right, Vector2.right,
                                             Vector2.up, Vector2.right, Vector2.right })
        {
            controls.Orbit(direction, 2f);
            controls.UpdateFraming();
            foreach (float x in new[] { -12.6f, 12.6f })
            foreach (float y in new[] { -1f, 1f })
            foreach (float z in new[] { -12.6f, 12.6f })
            {
                Vector3 point = camera.WorldToViewportPoint(new Vector3(x, y, z));
                framed &= point.z > 0f && point.x >= 0f && point.x <= 1f && point.y >= 0f && point.y <= 1f;
            }
            Vector3 top = camera.WorldToViewportPoint(new Vector3(0f, 8f, 7f));
            framed &= top.z > 0f && top.x >= 0f && top.x <= 1f && top.y >= 0f && top.y <= 1f;
        }
        Check(framed, "Arena and jump height stay in frame at different camera angles");
        controls.Orbit(Vector2.up, 100f);
        Check(camera.transform.position.y > 30f && camera.transform.position.y < 31f,
            "Camera elevation is limited before it flips upside down");
        controls.ResetView();
        Check(Vector3.Distance(initialPosition, camera.transform.position) < 0.001f
            && Quaternion.Angle(initialRotation, camera.transform.rotation) < 0.01f
            && Mathf.Abs(initialFov - camera.fieldOfView) < 0.01f,
            "Camera reset restores the original position, rotation, and framing");
        yield return new WaitForEndOfFrame();
        bool labelsFaceCamera = true;
        foreach (TextMesh label in FindObjectsByType<TextMesh>(FindObjectsSortMode.None))
            labelsFaceCamera &= Quaternion.Angle(label.transform.rotation, camera.transform.rotation) < 0.01f;
        Check(labelsFaceCamera, "Pad labels face the camera after moving and resetting");
        Capture(Path.Combine(output, "perspective-view.png"));''')
source = source.replace('Fixed perspective camera is active', 'Perspective camera controls are active')
path.write_text(source, encoding='utf-8')
print('Added isolated runtime checks for camera movement, bounds, labels, and reset.')

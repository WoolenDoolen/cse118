from pathlib import Path

path = Path(__file__).resolve().parent / 'verify-project/Assets/Scripts/PlaygroundVerification.cs'
source = path.read_text(encoding='utf-8')
marker = '        PlayerController player = FindAnyObjectByType<PlayerController>();'
assert marker in source
source = source.replace(marker, '''        Check(!controls.HardMode, "Hard mode starts disabled");
        Vector3 startAngles = camera.transform.eulerAngles;
        controls.SetHardMode(true);
        yield return new WaitForSeconds(0.25f);
        Vector3 rotatingAngles = camera.transform.eulerAngles;
        Check(controls.HardMode
            && Mathf.Abs(Mathf.DeltaAngle(startAngles.x, rotatingAngles.x)) > 1f
            && Mathf.Abs(Mathf.DeltaAngle(startAngles.y, rotatingAngles.y)) > 1f
            && Mathf.Abs(Mathf.DeltaAngle(startAngles.z, rotatingAngles.z)) > 1f,
            "Hard mode automatically rotates every axis without arrow input");
        controls.enabled = false;
        controls.AdvanceHardMode(1.5f);
        controls.UpdateFraming();
        Capture(Path.Combine(output, "hard-mode-view.png"));

        controls.ResetView();
        controls.SetHardMode(true);
        controls.AdvanceHardMode(1f);
        Quaternion oneStepRotation = camera.transform.rotation;
        Vector3 oneStepPosition = camera.transform.position;
        controls.ResetView();
        controls.SetHardMode(true);
        for (int i = 0; i < 100; i++)
            controls.AdvanceHardMode(0.01f);
        Check(Quaternion.Angle(oneStepRotation, camera.transform.rotation) < 0.05f
            && Vector3.Distance(oneStepPosition, camera.transform.position) < 0.01f,
            "Hard mode rotation speed is independent of frame rate");

        bool flipped = false;
        bool rolled = false;
        bool hardModeFits = true;
        for (int i = 0; i < 200; i++)
        {
            controls.AdvanceHardMode(0.2f);
            controls.UpdateFraming();
            flipped |= camera.transform.position.y < -1f;
            rolled |= Vector3.Dot(camera.transform.up, Vector3.up) < 0f;
            foreach (float x in new[] { -12.6f, 12.6f })
            foreach (float y in new[] { -1f, 1f })
            foreach (float z in new[] { -12.6f, 12.6f })
            {
                Vector3 point = camera.WorldToViewportPoint(new Vector3(x, y, z));
                hardModeFits &= point.z > 0f && point.x >= 0f && point.x <= 1f && point.y >= 0f && point.y <= 1f;
            }
        }
        Check(flipped && rolled && hardModeFits,
            "Hard mode completes full flips and rolls while keeping the arena in frame");
        controls.SetHardMode(false);
        Check(!controls.HardMode
            && Vector3.Distance(initialPosition, camera.transform.position) < 0.001f
            && Quaternion.Angle(initialRotation, camera.transform.rotation) < 0.01f,
            "Toggling hard mode off restores the normal view");
        controls.enabled = true;
        yield return new WaitForSeconds(0.2f);
        Check(Quaternion.Angle(initialRotation, camera.transform.rotation) < 0.01f,
            "Automatic camera rotation stops when hard mode is off");
        controls.SetHardMode(true);
        controls.AdvanceHardMode(3f);
        controls.ResetView();
        Check(!controls.HardMode
            && Vector3.Distance(initialPosition, camera.transform.position) < 0.001f
            && Quaternion.Angle(initialRotation, camera.transform.rotation) < 0.01f,
            "Camera reset exits hard mode and clears all rotation axes");

''' + marker)
path.write_text(source, encoding='utf-8')
print('Added isolated hard mode runtime checks.')

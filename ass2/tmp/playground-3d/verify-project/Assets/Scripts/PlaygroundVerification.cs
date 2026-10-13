using System;
using System.Collections;
using System.IO;
using UnityEngine;

public class PlaygroundVerification : MonoBehaviour
{
    static string output;

    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.AfterSceneLoad)]
    static void Attach()
    {
        string[] args = Environment.GetCommandLineArgs();
        for (int i = 0; i < args.Length - 1; i++)
            if (args[i] == "-verification-out")
                output = args[i + 1];
        if (!string.IsNullOrEmpty(output))
            Application.runInBackground = true;
        if (!string.IsNullOrEmpty(output))
            new GameObject("Verification").AddComponent<PlaygroundVerification>();
    }

    void Check(bool condition, string name)
    {
        if (!condition)
        {
            File.AppendAllText(Path.Combine(output, "runtime-checks.txt"), "FAIL: " + name + "\n");
            Debug.LogError("VERIFY FAIL: " + name);
            Application.Quit(1);
            throw new Exception(name);
        }
        File.AppendAllText(Path.Combine(output, "runtime-checks.txt"), "PASS: " + name + "\n");
        Debug.Log("VERIFY PASS: " + name);
    }

    void Capture(string path)
    {
        Camera camera = Camera.main;
        int width = Screen.width;
        int height = Mathf.Max(1, Screen.height - (int)PlaygroundCamera.HudHeight);
        RenderTexture texture = new RenderTexture(width, height, 24);
        RenderTexture oldTarget = camera.targetTexture;
        RenderTexture oldActive = RenderTexture.active;
        Rect oldRect = camera.rect;
        camera.targetTexture = texture;
        camera.rect = new Rect(0, 0, 1, 1);
        camera.aspect = (float)width / height;
        camera.Render();
        RenderTexture.active = texture;
        Texture2D pixels = new Texture2D(width, height, TextureFormat.RGB24, false);
        pixels.ReadPixels(new Rect(0, 0, width, height), 0, 0);
        pixels.Apply();
        File.WriteAllBytes(path, pixels.EncodeToPNG());
        camera.targetTexture = oldTarget;
        camera.rect = oldRect;
        camera.ResetAspect();
        RenderTexture.active = oldActive;
        texture.Release();
        Destroy(texture);
        Destroy(pixels);
    }

    IEnumerator Start()
    {
        Directory.CreateDirectory(output);
        yield return new WaitForSeconds(1f);
        Camera camera = Camera.main;
        Check(!camera.orthographic && camera.GetComponent<PlaygroundCamera>() != null,
            "Perspective camera controls are active");
        Light sun = GameObject.Find("Sun").GetComponent<Light>();
        Check(sun.shadows == LightShadows.Soft && QualitySettings.shadowDistance >= 60f,
            "Real-time ground shadows are enabled");
        Check(camera.rect.yMax < 1f && camera.rect.y == 0f, "HUD is above the camera viewport");

        Collider platform = GameObject.Find("Raised Platform").GetComponent<Collider>();
        Check(Mathf.Abs(platform.bounds.min.y) < 0.01f && Mathf.Abs(platform.bounds.max.y - 3f) < 0.01f,
            "Platform is solid from the floor to its original landing height");
        PlaygroundCamera controls = camera.GetComponent<PlaygroundCamera>();
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
        Capture(Path.Combine(output, "perspective-view.png"));
        yield return null;

        Check(!controls.HardMode, "Hard mode starts disabled");
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

        PlayerController player = FindAnyObjectByType<PlayerController>();
        player.enabled = false;
        Rigidbody body = player.GetComponent<Rigidbody>();
        body.position = new Vector3(0f, 0.65f, 3f);
        body.linearVelocity = Vector3.zero;
        float highest = 0f;
        for (int i = 0; i < 180; i++)
        {
            yield return new WaitForFixedUpdate();
            Vector3 velocity = body.linearVelocity;
            velocity.z = body.position.z < 7f ? 6f : 0f;
            body.linearVelocity = velocity;
            highest = Mathf.Max(highest, body.position.y);
            if (i == 35)
            {
                yield return new WaitForEndOfFrame();
                Capture(Path.Combine(output, "jump-height.png"));
            }
        }
        Check(highest > 6f && Mathf.Abs(body.position.y - 3.5f) < 0.15f,
            "Jump pad launches the player and lands them on the raised platform");
        Check(FindObjectsByType<Pickup>(FindObjectsSortMode.None).Length == 4,
            "The high gold cube can still be collected during landing");

        body.position = new Vector3(-8f, 0.65f, 2f);
        body.linearVelocity = Vector3.zero;
        yield return new WaitForFixedUpdate();
        yield return new WaitForFixedUpdate();
        yield return new WaitForFixedUpdate();
        Check(Mathf.Abs(body.position.x - 8f) < 0.2f, "Purple pad still teleports across the arena");

        Screen.SetResolution(600, 800, false);
        yield return new WaitForSeconds(0.5f);
        camera.GetComponent<PlaygroundCamera>().UpdateFraming();
        bool fits = true;
        foreach (float x in new[] { -12.6f, 12.6f })
        foreach (float y in new[] { -1f, 1f })
        foreach (float z in new[] { -12.6f, 12.6f })
        {
            Vector3 point = camera.WorldToViewportPoint(new Vector3(x, y, z));
            fits &= point.x >= 0f && point.x <= 1f && point.y >= 0f && point.y <= 1f;
        }
        Check(fits, "Arena stays in frame after resizing to a narrow window");
        yield return new WaitForEndOfFrame();
        Capture(Path.Combine(output, "narrow-view.png"));
        yield return null;
        yield return new WaitForSeconds(0.2f);
        Debug.Log("VERIFY COMPLETE");
        Application.Quit(0);
    }
}

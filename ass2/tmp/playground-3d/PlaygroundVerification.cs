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

    IEnumerator Start()
    {
        Directory.CreateDirectory(output);
        yield return new WaitForSeconds(1f);
        Camera camera = Camera.main;
        Check(!camera.orthographic && camera.GetComponent<PlaygroundCamera>() != null,
            "Fixed perspective camera is active");
        Light sun = GameObject.Find("Sun").GetComponent<Light>();
        Check(sun.shadows == LightShadows.Soft && QualitySettings.shadowDistance >= 60f,
            "Real-time ground shadows are enabled");
        Check(camera.rect.yMax < 1f && camera.rect.y == 0f, "HUD is above the camera viewport");

        Collider platform = GameObject.Find("Raised Platform").GetComponent<Collider>();
        Check(Mathf.Abs(platform.bounds.min.y) < 0.01f && Mathf.Abs(platform.bounds.max.y - 3f) < 0.01f,
            "Platform is solid from the floor to its original landing height");
        yield return new WaitForEndOfFrame();
        ScreenCapture.CaptureScreenshot(Path.Combine(output, "perspective-view.png"));
        yield return null;

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
                ScreenCapture.CaptureScreenshot(Path.Combine(output, "jump-height.png"));
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
        ScreenCapture.CaptureScreenshot(Path.Combine(output, "narrow-view.png"));
        yield return null;
        yield return new WaitForSeconds(0.2f);
        Debug.Log("VERIFY COMPLETE");
        Application.Quit(0);
    }
}

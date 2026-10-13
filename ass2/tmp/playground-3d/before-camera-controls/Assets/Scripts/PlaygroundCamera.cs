using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.Rendering;

[RequireComponent(typeof(Camera))]
public class PlaygroundCamera : MonoBehaviour
{
    public const float HudHeight = 100f;
    Camera view;

    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.AfterSceneLoad)]
    static void Initialize()
    {
        SceneManager.sceneLoaded -= OnSceneLoaded;
        SceneManager.sceneLoaded += OnSceneLoaded;
        ApplyScenePresentation(SceneManager.GetActiveScene());
    }

    static void OnSceneLoaded(Scene scene, LoadSceneMode mode)
    {
        ApplyScenePresentation(scene);
    }

    public static void ApplyScenePresentation(Scene scene)
    {
        if (!scene.IsValid() || !scene.isLoaded)
            return;

        GameObject[] roots = scene.GetRootGameObjects();
        bool playground = false;
        Camera camera = null;
        foreach (GameObject root in roots)
        {
            if (root.GetComponent<GameManager>() != null)
                playground = true;
            if (root.CompareTag("MainCamera"))
                camera = root.GetComponent<Camera>();
        }
        if (!playground || camera == null)
            return;

        PlaygroundCamera framing = camera.GetComponent<PlaygroundCamera>();
        if (framing == null)
            framing = camera.gameObject.AddComponent<PlaygroundCamera>();
        framing.Configure();

        RenderSettings.ambientMode = AmbientMode.Flat;
        RenderSettings.ambientLight = new Color(0.3f, 0.32f, 0.36f);
        QualitySettings.shadows = ShadowQuality.All;
        QualitySettings.shadowDistance = 80f;
        QualitySettings.shadowResolution = ShadowResolution.High;
        QualitySettings.shadowCascades = 2;
        QualitySettings.antiAliasing = 4;

        foreach (GameObject root in roots)
        {
            Light light = root.GetComponent<Light>();
            if (light != null && light.type == LightType.Directional)
            {
                light.shadows = LightShadows.Soft;
                light.shadowStrength = 0.85f;
                light.shadowBias = 0.03f;
                light.shadowNormalBias = 0.15f;
                light.intensity = 1.15f;
                light.transform.rotation = Quaternion.Euler(50f, -30f, 0f);
            }

            if (root.name == "Raised Platform")
            {
                // Keep the landing surface at the same height, and fill down to the floor.
                float top = root.GetComponent<Collider>().bounds.max.y;
                Vector3 position = root.transform.position;
                Vector3 scale = root.transform.localScale;
                position.y = top * 0.5f;
                scale.y = top;
                root.transform.position = position;
                root.transform.localScale = scale;
            }

            TextMesh label = root.GetComponent<TextMesh>();
            if (label != null)
            {
                label.fontSize = 64;
                label.characterSize = 0.13f;
                label.transform.rotation = camera.transform.rotation;
            }
        }
    }

    void OnEnable()
    {
        Configure();
    }

    public void Configure()
    {
        view = GetComponent<Camera>();
        view.orthographic = false;
        view.nearClipPlane = 0.1f;
        view.farClipPlane = 100f;
        view.clearFlags = CameraClearFlags.SolidColor;
        view.backgroundColor = new Color(0.12f, 0.15f, 0.2f);
        transform.position = new Vector3(0f, 18f, -24f);
        transform.LookAt(new Vector3(0f, 0f, 1f));
        UpdateFraming();
    }

    void LateUpdate()
    {
        UpdateFraming();
    }

    public void UpdateFraming()
    {
        if (view == null)
            view = GetComponent<Camera>();

        // Reserve the top strip for the HUD instead of drawing it across the arena.
        float height = Mathf.Max(1f, Screen.height);
        float viewportHeight = Mathf.Clamp(1f - HudHeight / height, 0.3f, 1f);
        view.rect = new Rect(0f, 0f, 1f, viewportHeight);
        float aspect = Mathf.Max(0.1f, Screen.width / (height * viewportHeight));
        float tangent = Mathf.Tan(22f * Mathf.Deg2Rad);

        // Widen the view on narrow windows while keeping the same camera pose.
        for (int x = -1; x <= 1; x += 2)
        for (int y = -1; y <= 1; y += 2)
        for (int z = -1; z <= 1; z += 2)
        {
            Vector3 corner = transform.InverseTransformPoint(new Vector3(x * 12.6f, y, z * 12.6f));
            if (corner.z <= 0f)
                continue;
            tangent = Mathf.Max(tangent, Mathf.Abs(corner.y) / corner.z * 1.08f,
                Mathf.Abs(corner.x) / (corner.z * aspect) * 1.08f);
        }
        view.fieldOfView = Mathf.Clamp(2f * Mathf.Atan(tangent) * Mathf.Rad2Deg, 44f, 110f);
    }
}

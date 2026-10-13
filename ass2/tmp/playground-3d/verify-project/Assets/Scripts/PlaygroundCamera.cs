using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.Rendering;

[RequireComponent(typeof(Camera))]
public class PlaygroundCamera : MonoBehaviour
{
    public const float HudHeight = 100f;
    Camera view;
    TextMesh[] labels;
    float yaw;
    float pitch;
    float roll;
    public bool HardMode { get; private set; }
    static readonly Vector3 focus = new Vector3(0f, 0f, 1f);
    const float OrbitSpeed = 45f;

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
        labels = FindObjectsByType<TextMesh>(FindObjectsSortMode.None);
        ResetView();
    }

    void Update()
    {
        if (Input.GetKeyDown(KeyCode.L))
        {
            ResetView();
            return;
        }

        if (Input.GetKeyDown(KeyCode.H))
            SetHardMode(!HardMode);
        if (HardMode)
        {
            AdvanceHardMode(Time.deltaTime);
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
        ApplyOrbitPose();
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

    public void ResetView()
    {
        HardMode = false;
        roll = 0f;
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

        // Fit the arena and jump height from the current camera angle.
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
        Vector3 jumpTop = transform.InverseTransformPoint(new Vector3(0f, 8f, 7f));
        if (jumpTop.z > 0f)
            tangent = Mathf.Max(tangent, Mathf.Abs(jumpTop.y) / jumpTop.z * 1.08f,
                Mathf.Abs(jumpTop.x) / (jumpTop.z * aspect) * 1.08f);
        view.fieldOfView = Mathf.Clamp(2f * Mathf.Atan(tangent) * Mathf.Rad2Deg, 44f, 110f);
    }
}

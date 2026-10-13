using UnityEngine;

[RequireComponent(typeof(Rigidbody))]
public class PlayerController : MonoBehaviour
{
    public float speed = 6f;
    public float jumpSpeed = 6f;
    [HideInInspector] public float teleportReadyTime;

    Rigidbody body;
    Vector3 startPosition;
    Vector3 movement;
    bool jumpRequested;

    void Awake()
    {
        body = GetComponent<Rigidbody>();
        startPosition = transform.position;
    }

    void Update()
    {
        float x = (Input.GetKey(KeyCode.D) ? 1 : 0)
                - (Input.GetKey(KeyCode.A) ? 1 : 0);
        float z = (Input.GetKey(KeyCode.W) ? 1 : 0)
                - (Input.GetKey(KeyCode.S) ? 1 : 0);
        movement = Vector3.ClampMagnitude(new Vector3(x, 0, z), 1f);

        // Raycasting checks the ground before allowing a jump.
        // The player uses the Ignore Raycast layer, and triggers are ignored.
        bool grounded = Physics.Raycast(body.position, Vector3.down, 0.65f,
            Physics.DefaultRaycastLayers, QueryTriggerInteraction.Ignore);
        if (Input.GetKeyDown(KeyCode.Space) && grounded)
            jumpRequested = true;

        if (transform.position.y < -8f)
        {
            body.position = startPosition;
            body.linearVelocity = Vector3.zero;
        }
    }

    void FixedUpdate()
    {
        Vector3 velocity = body.linearVelocity;
        Vector3 horizontal = Vector3.MoveTowards(new Vector3(velocity.x, 0, velocity.z),
            movement * speed, 30f * Time.fixedDeltaTime);
        body.linearVelocity = new Vector3(horizontal.x,
            jumpRequested ? jumpSpeed : velocity.y, horizontal.z);
        jumpRequested = false;
    }
}

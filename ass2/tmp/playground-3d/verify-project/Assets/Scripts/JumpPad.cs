using UnityEngine;

public class JumpPad : MonoBehaviour
{
    public float launchSpeed = 12f;

    void OnTriggerEnter(Collider other)
    {
        if (!other.TryGetComponent<PlayerController>(out _))
            return;

        Rigidbody body = other.attachedRigidbody;
        Vector3 velocity = body.linearVelocity;
        body.linearVelocity = new Vector3(velocity.x, launchSpeed, velocity.z);
        FindAnyObjectByType<GameManager>().SetMessage("Jump pad! Move forward to land on the raised platform.");
    }
}

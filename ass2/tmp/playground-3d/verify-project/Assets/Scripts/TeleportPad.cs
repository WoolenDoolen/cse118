using UnityEngine;

public class TeleportPad : MonoBehaviour
{
    public Transform destination;

    void OnTriggerEnter(Collider other)
    {
        if (!other.TryGetComponent<PlayerController>(out var player)
            || destination == null || Time.time < player.teleportReadyTime)
            return;

        player.teleportReadyTime = Time.time + 1.5f;
        other.attachedRigidbody.position = destination.position + Vector3.up * 0.8f;
        other.attachedRigidbody.linearVelocity = Vector3.zero;
        FindAnyObjectByType<GameManager>().SetMessage("Teleported! Purple pads connect the two sides of the arena.");
    }
}

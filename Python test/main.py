from MinecraftWSClient import MinecraftWSClient
import time

def main():
    HOST = "127.0.0.1"
    PORT = 8765
    USERNAME = "admin"
    PASSWORD = "localpass"

    client = MinecraftWSClient(
        host=HOST,
        port=PORT,
        username=USERNAME,
        password=PASSWORD
    )

    try:
        # Connect & authenticate
        client.connect()
        print("Connected & authenticated")

        # ---- Send a raw command ----
        client.send_command("say Hello from Python WebSocket!")
        time.sleep(0.2)

        # ---- Get all player coordinates ----
        players = client.request_player_coords()
        print("Players online:")
        for name, data in players.items():
            print(f" - {name}: {data}")

        # ---- Run a command at a specific player's location ----
        if players:
            player_name = list(players.keys())[0]

            cmd = client.command_at_player_coords(
                player_name,
                "summon tnt {x} {y} {z}"
            )
            print("Executed:", cmd)

        # Keep connection alive briefly
        time.sleep(2)

    except Exception as e:
        print("Error:", e)

    finally:
        client.close()
        print("Connection closed")


if __name__ == "__main__":
    main()

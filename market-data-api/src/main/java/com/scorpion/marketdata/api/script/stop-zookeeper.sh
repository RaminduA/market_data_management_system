# Stop Zookeeper
echo "Stopping Zookeeper..."
cd C:\Kafka
.\bin\windows\zookeeper-server-stop.bat config\zookeeper.properties
echo "Zookeeper stopped successfully!"

from diagrams.custom import Custom
from diagrams.onprem.monitoring import Grafana
from diagrams.onprem.monitoring import Prometheus

from diagrams.aws.general import User

from diagrams import Diagram, Edge

with (Diagram("", show=False)):
    locust = Custom("Locust", "./rescources/locust.png")
    app = Custom("Spring Boot", "./rescources/springboot.png")

    metrics = Prometheus("Prometheus")
    monitoring = Grafana("Grafana")

    user = User("Admin")

    locust >> Edge(label="HTTP 요청 및 응답", color="darkgreen") >> app >> Edge(color="darkgreen") >> locust
    app << Edge(label="지표 수집", color="firebrick") << metrics << Edge(label="데이터 시각화", color="darkorange", style="dashed") << monitoring

    monitoring << Edge(label="대시보드 모니터링") << user

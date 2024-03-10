from diagrams.custom import Custom
from diagrams.generic.device import Mobile
from diagrams.onprem.client import Client
from diagrams.onprem.container import Docker
from diagrams.onprem.database import Mysql
from diagrams.onprem.inmemory import Redis
from diagrams.onprem.monitoring import Grafana
from diagrams.onprem.monitoring import Prometheus

from diagrams import Cluster, Diagram

with Diagram("My Pay", show=False):
    with Cluster("클라이언트"):
        client = Client("")
        mobile = Mobile("")
        [client, mobile]
        with Cluster("부하 테스트 도구"):
            locust = Custom("Locust", "./rescources/locust.png")

    with Cluster("도커"):
        Docker()

        with Cluster("애플리케이션"):
            with Cluster("API 게이트웨이"):
                app1 = Custom("api-gateway", "./rescources/springboot.png")

            with Cluster("머니 서비스"):
                app3 = Custom("money-service", "./rescources/springboot.png")
                app3 - Redis("Redis")
                node = app3 - Mysql("DB")

            with Cluster("계정 서비스"):
                app2 = Custom("account-service", "./rescources/springboot.png")
                app2 - Mysql("DB")

        with Cluster("모니터링"):
            metrics = Prometheus("Prometheus")
            metrics << Grafana("Grafana")

        client >> app1
        app1 >> [app2, app3]
        node << metrics

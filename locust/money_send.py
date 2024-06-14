from locust import HttpUser, task, between


# [LOCK:1, LOCK:2]
class UserA(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 5)

    @task
    def send_money(self):
        response = self.client.post("/money/v1/money/send",
                                    json={"receiverWalletId": 2, "amount": 100, "summary": "locust UserA"})
        print("UserA ::: send_money ::: response ::: ", response.text)

    def on_start(self):
        self.client.headers = {"Authorization": "Bearer 123"}


# [LOCK:2, LOCK:3]
# class UserB(HttpUser):
#     host = "http://localhost:8080"
#     wait_time = between(1, 5)
#
#     @task
#     def send_money(self):
#         response = self.client.post("/money/v1/money/send",
#                                     json={"receiverWalletId": 3, "amount": 100, "summary": "locust UserB"})
#         print("UserB ::: send_money ::: response ::: ", response.text)
#
#     def on_start(self):
#         self.client.headers = {"Authorization": "Bearer 456"}


# [LOCK:2, LOCK:1]
class UserB(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 5)

    @task
    def send_money(self):
        response = self.client.post("/money/v1/money/send",
                                    json={"receiverWalletId": 1, "amount": 100, "summary": "locust UserB"})
        print("UserB ::: send_money ::: response ::: ", response.text)

    def on_start(self):
        self.client.headers = {"Authorization": "Bearer 456"}

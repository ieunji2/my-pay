from locust import HttpUser, task, between, SequentialTaskSet, constant


def ping(self):
    self.client.get("/money/health/ping")


# class UserA(HttpUser):
#     host = "http://localhost:8080"
#     # wait_time = between(1, 5)
#     accountId = ""
#
#     @task
#     def find_account(self):
#         response = self.client.get("/accounts/v1/accounts/" + str(self.accountId))
#         print("UserA ::: find_account ::: response ::: ", response.text)
#
#     def on_start(self):
#         self.accountId = self.client.post("/accounts/v1/accounts",
#                                           json={"name": "foo", "email": "foo@mypay.com"}).json()["id"]
#         print("UserA ::: on_start ::: accountId ::: ", self.accountId)


class BTasks(SequentialTaskSet):
    # @task
    # def charge_money(self):
    #     response = self.client.post("/money/v1/money/charge",
    #                                 json={"amount": 2000, "summary": "locust UserB"})
    #     print("UserB ::: charge_money ::: response ::: ", response.text)

    @task
    def send_money(self):
        response = self.client.post("/money/v1/money/send",
                                    json={"receiverWalletId": 2, "amount": 200, "summary": "lucky"})
        print("UserB ::: send_money ::: response ::: ", response.text)


class UserB(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 5)
    # wait_time = constant(5)

    tasks = [BTasks]

    def on_start(self):
        self.client.headers = {"Authorization": "Bearer 123"}


class CTasks(SequentialTaskSet):
    # @task
    # def charge_money(self):
    #     response = self.client.post("/money/v1/money/charge",
    #                                 json={"amount": 2000, "summary": "locust UserC"})
    #     print("UserC ::: charge_money ::: response ::: ", response.text)

    @task
    def send_money(self):
        response = self.client.post("/money/v1/money/send",
                                    json={"receiverWalletId": 1, "amount": 100, "summary": "good"})
        print("UserC ::: send_money ::: response ::: ", response.text)


class UserC(HttpUser):
    host = "http://localhost:8080"
    wait_time = between(1, 5)
    # wait_time = constant(5)

    tasks = [CTasks]

    def on_start(self):
        self.client.headers = {"Authorization": "Bearer 456"}


# class UserD(HttpUser):
#     host = "http://localhost:8080"
#     # wait_time = between(1, 3)
#     wait_time = constant(5)
#
#     @task
#     def send_money(self):
#         response = self.client.post("/money/v1/money/send",
#                                     json={"receiverWalletId": 1, "amount": 100, "summary": "hello"})
#         print("UserD ::: send_money ::: response ::: ", response.text)
#
#     def on_start(self):
#         self.client.headers = {"Authorization": "Bearer 789"}

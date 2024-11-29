<template>
    <div class="container mt-4">
      <h5 class="mb-5"><b>History</b></h5>
      <!-- 요청 기록 테이블 -->
      <div class="table-responsive mt-4">
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Method</th>
              <th>URL</th>
              <th>Response Code</th>
              <th>Response Time (ms)</th>
              <th style="border-right: 0;">Logged Time</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(log, index) in logs" :key="index">
              <td style="text-align: center;">
                <span :class="'badge bg-' + log.methodColor">{{
                  log.method
                }}</span>
              </td>
              <td>{{ log.url }}</td>
              <td>{{ log.responseCode }} {{ log.responseMessage }}</td>
              <td>{{ log.responseTime }}</td>
              <td style="border-right: 0;">{{ log.loggedTime }}</td>
              <td>
                <button class="btn btn-light" @click="toggleDetails(index)">
                  <i
                    class="bi"
                    :class="
                      detailsIndex === index ? 'bi-chevron-up' : 'bi-chevron-down'
                    "
                  ></i>
                </button>
              </td>
            </tr>
            <!-- 상세 페이지 -->
            <tr v-if="detailsIndex === index">
              <td colspan="6">
                <div class="p-3 border">
                  <h6>Details</h6>
                  <hr />
                  <p><b>Method:</b> {{ logs[detailsIndex].method }}</p>
                  <p><b>URL:</b> {{ logs[detailsIndex].url }}</p>
                  <p><b>Header:</b> {{ logs[detailsIndex].header }}</p>
                  <p><b>Parameter:</b> {{ logs[detailsIndex].parameter }}</p>
                  <hr />
                  <div class="row">
                    <div class="col-md-6">
                      <h6>Response Body</h6>
                      <pre>{{ logs[detailsIndex].responseBody }}</pre>
                    </div>
                    <div class="col-md-6">
                      <h6>Response Header</h6>
                      <pre>{{ logs[detailsIndex].responseHeader }}</pre>
                    </div>
                  </div>
                  <div class="d-flex justify-content-end mt-3">
                    <button
                      class="btn btn-dark me-2"
                      @click="reRequest(detailsIndex)"
                    >
                      다시 요청하기
                    </button>
                    <button
                      class="btn btn-secondary"
                      @click="editRequest(detailsIndex)"
                    >
                      수정하기
                    </button>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ReportVue",
    data() {
      return {
        logs: [
          {
            method: "POST",
            methodColor: "secondary",
            url: "http://localhost:8082",
            responseCode: 200,
            responseMessage: "OK",
            responseTime: 150,
            loggedTime: "2024-11-27 12:00",
            header: "Authorization: Bearer xyz",
            parameter: "userId: 123",
            responseBody: `{
                            "id": 123,
                            "name": "John Doe",
                            "email": "john@example.com"
                            }`,
            responseHeader: `Content-type: application/json
                            Cache-Control: no-cache`,
          },
          {
            method: "GET",
            methodColor: "secondary",
            url: "http://localhost:8083",
            responseCode: 404,
            responseMessage: "Not Found",
            responseTime: 250,
            loggedTime: "2024-11-27 12:05",
            header: "Authorization: Bearer abc",
            parameter: "query: test",
            responseBody: `{
                            "error": "Not Found"
                            }`,
            responseHeader: `Content-type: application/json
                            Cache-Control: no-cache`,
          },
        ],
        detailsIndex: null, // 선택된 요청 상세 정보 인덱스
      };
    },
    methods: {
      toggleDetails(index) {
        this.detailsIndex = this.detailsIndex === index ? null : index;
      },
      reRequest(index) {
        alert(`다시 요청하기: ${this.logs[index].url}`);
      },
      editRequest(index) {
        alert(`수정하기: ${this.logs[index].url}`);
      },
    },
  };
  </script>
  
  <style scoped>
  .badge {
    font-size: 0.9rem;
    padding: 0.5rem;
  }
  
  pre {
    background-color: #f8f9fa;
    padding: 1rem;
    border-radius: 0.5rem;
    font-size: 0.9rem;
    overflow: auto;
  }
  
  table th {
    text-align: center;
  }
  
  .table th,
  .table td {
    vertical-align: middle;
  }
  
  table th:last-child,
  table td:last-child {
    text-align: right;
  }
  </style>
  
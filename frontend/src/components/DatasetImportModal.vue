<template>
  <div class="modal-backdrop">
    <div class="modal-container">
      <div class="modal-header">
        <h3>Import</h3>
        <button class="close-button" @click="$emit('close')">×</button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <!-- Dataset Name -->
          <div class="form-group">
            <label for="datasetName">Dataset Name</label>
            <input
              id="datasetName"
              type="text"
              class="form-control"
              v-model="datasetName"
              placeholder="Enter dataset name"
              required
            />
          </div>

          <!-- Description -->
          <div class="form-group">
            <label for="description">Description</label>
            <textarea
              id="description"
              class="form-control"
              v-model="description"
              placeholder="Enter description"
              rows="3"
            ></textarea>
          </div>

          <!-- Java Code Input -->
          <div class="form-group">
            <label>다음과 같은 형식의 코드를 입력해 주세요.</label>
            <pre class="code-example">
                private Long id; // 프로젝트 id
                private String name; // 프로젝트 이름
                private String createAt; // 생성 일자
              </pre
            >
            <textarea
              class="form-control code-input"
              v-model="codeInput"
              placeholder="Paste your code here"
              rows="6"
              required
            ></textarea>
          </div>

          <button type="submit" class="btn btn-primary w-100 mt-3">추가</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "DatasetModal",
  data() {
    return {
      datasetName: "",
      description: "",
      codeInput: "",
    };
  },
  methods: {
    handleSubmit() {
      if (!this.datasetName.trim() || !this.codeInput.trim()) {
        alert("Dataset Name과 코드를 입력해 주세요.");
        return;
      }

      const payload = {
        datasetName: this.datasetName.trim(),
        description: this.description.trim(),
        codeInput: this.codeInput.trim(),
      };

      this.$emit("save-dataset", payload);
      this.$emit("close");
    },
    resetForm() {
      this.datasetName = "";
      this.description = "";
      this.codeInput = "";
    },
  },
};
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 400px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.modal-header h3 {
  margin: 0;
}

.close-button {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 15px;
}

.code-example {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  color: #d63384;
  margin-bottom: 10px;
}

textarea.code-input {
  font-family: monospace;
}
</style>

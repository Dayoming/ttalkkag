<template>
  <div v-if="isVisible" class="modal fade show" id="commonModal" tabindex="-1" role="dialog" aria-labelledby="commonModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ title }}</h5>
          <button type="button" class="btn-close" @click="closeModal"></button>
        </div>
        <div class="modal-body">
          <slot name="body"></slot>
          <p v-if="showError" class="text-danger mt-2">{{ errorMessage }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">Close</button>
          <button class="btn btn-primary" @click="confirmAction">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CommonModal",
  props: {
    isVisible: Boolean,
    title: String,
    confirmText: String,
  },
  emits: ["confirm", "close"],
  methods: {
    closeModal() {
      this.$emit("close");
    },
    confirmAction() {
      this.$emit("confirm");
    },
  },
};
</script>

<style scoped>
.modal.show {
  display: block;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn {
  min-width: 80px;
}

</style>

<template>
    <div
      class="modal modal-backdrop"
      :class="{ 'd-none': !isVisible }"
      role="dialog"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ title }}</h5>
            <button class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <slot name="body">{{ body }}</slot>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">Close</button>
            <button
              v-if="confirmText"
              class="btn btn-primary"
              @click="confirmAction"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "CommonModal",
    props: {
      title: { type: String, required: true },
      body: { type: String, default: "" },
      confirmText: { type: String, default: null },
      isVisible: { type: Boolean, required: true },
    },
    emits: ["confirm", "close"],
    methods: {
      closeModal() {
        this.$emit("close");
      },
      confirmAction() {
        this.$emit("confirm");
        this.closeModal();
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
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1050;
  }
  .modal-backdrop.d-none {
    display: none;
  }
  .modal-dialog {
    max-width: 500px;
    width: 100%;
  }
  </style>
  
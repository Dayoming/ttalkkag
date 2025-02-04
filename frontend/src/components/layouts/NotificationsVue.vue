<template>
    <div>
      <div v-if="notifications.length > 0" class="alert alert-info">
        <ul>
          <li v-for="(notification, index) in notifications" :key="index">
            {{ notification }}
          </li>
        </ul>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "NotificationsVue",
    data() {
      return {
        notifications: [],
      };
    },
    methods: {
      handleNotification(notification) {
        this.notifications.push(notification);
        setTimeout(() => {
          this.notifications.shift(); // 일정 시간 후 알림 제거
        }, 5000);
      },
    },
  };
  </script>
  
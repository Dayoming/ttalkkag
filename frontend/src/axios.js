import axios from 'axios';

const instance = axios.create({
    baseURL: `http://${process.env.VUE_APP_SERVER_IP}`,
});

export default instance;

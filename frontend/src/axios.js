import axios from 'axios';

const instance = axios.create({
    baseURL: `http://192.168.50.63:8081`,
});

export default instance;

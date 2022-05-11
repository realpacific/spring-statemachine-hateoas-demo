import axios from "axios";

const defaultOptions = {
    baseURL: "http://localhost:8080/",
    headers: {
        "Content-Type": "application/json",
        'Accept': 'application/prs.hal-forms+json'
    }
};
const defaultAxios = axios.create(defaultOptions);

export default defaultAxios
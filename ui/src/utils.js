import axios from "axios";

export const buildRequestFromLink = ({type, href, data}) => {
    return axios({
        method: type,
        url: href,
        data: data
    })
}

export const format = (state) => {
    return state?.replaceAll("_", " ") || ""
}
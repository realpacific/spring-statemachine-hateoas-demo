import axios from "axios";

export const buildRequestFromLink = ({type, href, data}) => {
    return axios({
        method: type,
        url: href,
        data: data
    })
}

export const parseState = (state) => {
    return state.replaceAll("_", " ")
}
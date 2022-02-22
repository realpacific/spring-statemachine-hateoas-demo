import {useEffect, useState} from "react";
import axios from "axios";
import React from "react";
import {useParams} from "react-router-dom";
import Button from "../components/Button";
import {buildRequestFromLink, parseState} from "../utils";

const BASE_URL = "http://localhost:8080/articles"

const ArticleDetail = (props) => {
    const {id} = useParams();
    const [article, setArticle] = useState(null);
    const [tasks, setTasks] = useState(null);

    const fetchTasks = async (link) => {
        const response = await buildRequestFromLink(link)
        const data = response.data.sort((a, b) => a.rel.localeCompare(b.rel))
        setTasks(data)
    }
    const fetchArticleDetail = async () => {
        const response = await axios.get(BASE_URL + '/' + id)
        const data = response.data
        setArticle(data)
        await fetchTasks(data._links.tasks)
    }
    const handleTaskClick = async (task) => {
        const response = await buildRequestFromLink(task)
        if (response.status === 200) await fetchTasks(article._links.tasks)
    }

    useEffect(() => {
        (async () => await fetchArticleDetail())()
    }, [])

    return (
        <>
            {article != null ?
                <>
                    <div style={{
                        display: 'flex',
                        justifyContent: 'flex-end'
                    }}>
                        {tasks ? tasks.map(it =>
                            <Button
                                title={parseState(it.rel)} className={'btn btn-default'}
                                onClick={() => handleTaskClick(it)}
                            />
                        ) : <></>}

                    </div>
                    <h1>{article.title}</h1>
                    <span>{parseState(article.state)}</span>
                    <p>{article.body}</p>
                </> : <></>
            }
        </>
    )
}

export default ArticleDetail;
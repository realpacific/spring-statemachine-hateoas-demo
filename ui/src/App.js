import {Routes, Route, Link} from "react-router-dom";
import ArticleList from "./pages/ArticleList";
import ArticleDetail from "./pages/ArticleDetail";
import {LoaderContext} from "./AppContext";
import {useState} from "react";

const App = () => {
    const [isLoading, setIsLoading] = useState(true)
    return (
        <LoaderContext.Provider value={[isLoading, setIsLoading]}>
            <nav className="navbar navbar-dark">
                <Link to='/'><h2 className="navbar-brand mx-2">raywenderlich.com</h2></Link>
            </nav>
            {isLoading && (<div className="progress border-0">
                <div className="progress-bar progress-bar-striped progress-bar-animated w-100 h-25"/>
            </div>)}
            <div className="container-fluid mt-2">
                <Routes>
                    <Route path="/" element={<ArticleList/>}/>
                    <Route path="/:id" element={<ArticleDetail/>}/>
                </Routes>
            </div>
        </LoaderContext.Provider>
    );
}

export default App;

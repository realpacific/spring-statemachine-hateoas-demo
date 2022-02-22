import {Routes, Route} from "react-router-dom";
import ArticleList from "./pages/ArticleList";
import ArticleDetail from "./pages/ArticleDetail";

const App = () => {
    return (
        <>
            <nav className='navbar navbar-dark bg-dark'>
                <h2 className='navbar-brand'>raywenderlich.com</h2>
            </nav>
            <div className='container-fluid'>
                <Routes>
                    <Route path="/" element={<ArticleList/>}/>
                    <Route path="/:id" element={<ArticleDetail/>}/>
                </Routes>
            </div>
        </>
    );
}

export default App;

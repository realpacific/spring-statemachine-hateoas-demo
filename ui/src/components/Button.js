const Button = ({className, title, onClick}) => {
    return (
        <button
            type='button'
            className={`btn + ${className}`}
            onClick={onClick}>{title}</button>
    );
}

export default Button;

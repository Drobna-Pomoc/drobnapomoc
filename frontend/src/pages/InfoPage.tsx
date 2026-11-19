import { useParams } from 'react-router-dom';

const InfoPage = () => {
    const { slug } = useParams();
    return (
        <div style={{ padding: '40px', textAlign: 'center'}}>
            <h1>Podstrona informacyjna</h1>
            <div>Informacje o sekcji: {slug}</div>
        </div>
    );
};

export default InfoPage;
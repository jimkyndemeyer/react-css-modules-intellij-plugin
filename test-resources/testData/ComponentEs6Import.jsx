import * as React from 'react';
import styles from "./Component.css";

export class Component1 extends React.Component {
    render() {
        return (
            <div className={styles['<caret>']}></div>
        );
    }
}
import * as React from 'react';
const styles = require("./Component.css");

export class Component1 extends React.Component {
    render() {
        const className = styles['nor<caret>'];
        return (
            <div className={className}></div>
        );
    }
}
import * as React from 'react';
const styles = require("./Component.css");

export class Component1 extends React.Component {
    render() {
        const className = styles['<caret>'];
        return (
            <div className={className}></div>
        );
    }
}
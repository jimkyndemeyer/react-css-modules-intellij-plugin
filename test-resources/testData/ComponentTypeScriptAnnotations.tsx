import * as React from 'react';
const styles = require("./Component.css");

export class Component1 extends React.Component<any, any> {
    render() {
        const normal = styles['normal'];
        const invalid = styles['<error descr="Unknown class name \"invalid\"">invalid</error>'];
        return (
            <div styleName="normal <error descr="Unknown class name \"invalid\"">invalid</error>"></div>
        );
    }
}
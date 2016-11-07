import * as React from 'react';
const styles = require("./ComponentFindUsages.css");

export class Component1 extends React.Component {
    render() {
        const normal = styles['normalusage'];
        return (
            <div styleName="foo normalusage baz"></div>
        );
    }
}
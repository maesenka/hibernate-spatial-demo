const path = require( 'path' );

module.exports = {
    mode: process.env.NODE_ENV === 'development' ? 'development' : 'production',
    entry: './main.js',
    'devtool': 'eval-source-map',
    output: {
        path: path.resolve(__dirname, '../src/main/resources/static'),
        filename: 'bundle.js'
    },
};
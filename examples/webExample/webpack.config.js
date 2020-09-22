const path = require('path');

module.exports = {
    entry: './src/index.ts',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'dist')
    },
    optimization: {
        minimize: false
    },
    resolve: {
        symlinks: false
    },
    mode: 'production',
    performance: { hints: false },
};

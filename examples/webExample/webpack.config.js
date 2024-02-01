const path = require('path');

module.exports = {
    entry: './src/index.ts',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'dist')
    },
    devServer: {
        static: {
          directory: path.join(__dirname, 'dist'),
        },
        compress: true,
        port: 9000,
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

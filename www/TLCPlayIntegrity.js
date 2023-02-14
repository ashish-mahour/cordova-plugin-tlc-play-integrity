var exec = require('cordova/exec');

exports.certifyKey = (nonce, success, error) => {
    exec(success, error, 'TLCPlayIntegrity', 'certifyKey', [nonce]);
};
var InAppUpdate = {};

InAppUpdate.check = function(success, error) {
    cordova.exec(success, error, 'InAppUpdatePlugin', 'check', []);
};

InAppUpdate.update = function(success, error, config) {
    cordova.exec(success, error, 'InAppUpdatePlugin', 'update', [config]);
};

InAppUpdate.completeFlexibleUpdate = function(success, error, config) {
    cordova.exec(success, error, 'InAppUpdatePlugin', 'completeFlexibleUpdate', []);
};

module.exports = InAppUpdate;
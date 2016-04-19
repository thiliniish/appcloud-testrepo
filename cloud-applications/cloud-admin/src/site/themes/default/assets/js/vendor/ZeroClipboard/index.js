
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Module exports.
 */

exports = module.exports = setup;

/**
 * Module dependencies.
 */

var http = require('http');
var send = require('send');
var root = __dirname;
var swf = '/ZeroClipboard.swf';

function setup () {
  return http.createServer(onReq);
}

function onReq (req, res) {
  send(req, swf)
    .root(root)
    .on('error', onError)
    .pipe(res);
}

function onError (err) {
  res.statusCode = err.status || 500;
  res.end(err.message);
}

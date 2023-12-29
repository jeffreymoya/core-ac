local http = require "resty.http"
local cjson = require "cjson.safe"

local Utils = {}

function Utils.checkPermission(user, routePath, httpMethod, plugin_conf)
  local httpc = http.new()
  httpc:set_timeout(5000)

  local res, err = httpc:request_uri(plugin_conf.pdp_endpoint, {
    method = "POST",
    headers = {
      ["Content-Type"] = "application/json",
      [plugin_conf.auth_user_header] = user,
    },
    body = cjson.encode({ route = routePath, method = httpMethod, uri_template = plugin_conf.uri_template }),
  })

  if not res then
    kong.log.err("Failed to make API request: ", err)
    return false
  end

  kong.log.debug("Received response from: ", plugin_conf.pdp_endpoint)
  kong.log.debug("Response status: ", res.status)
  kong.log.debug("Response headers: ", cjson.encode(res.headers))
  kong.log.debug("Response body: ", res.body)

  if res.status == 500 then
    kong.log.err("Server error when checking permission: ", res.body)
    return false
  end

  if res.status == 200 then
    return true
  end

  return false
end

return Utils

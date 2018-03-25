function send_subtitle(name, value)
    if not name == "sub-text" then return end
    if not value then value = "" end
    -- no connection is necessary, just send the subtitle
    udp:send(value)
end

socket = require("socket")
udp = assert(socket.udp())
assert(udp:setpeername("127.0.0.1", 17827))

mp.observe_property("sub-text", "string", send_subtitle)
mp.set_property_bool("sub-visibility", true)
-- prevent subtitles from being shown locally
mp.set_property("sub-color", "#00000000")
mp.set_property_number("sub-border-size", 0)

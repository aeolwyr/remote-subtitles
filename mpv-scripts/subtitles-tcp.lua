function send_subtitle(name, value)
    if not name == "sub-text" then return end
    if not value then value = "" end
    -- try to send the subtitle, append a null byte at the end
    if not tcp or not tcp:send(value .. "\0") then
	-- connection error, try to reconnect
        tcp = socket.connect("127.0.0.1", 17827)
    end
end

socket = require("socket")
tcp = nil

mp.observe_property("sub-text", "string", send_subtitle)
mp.set_property_bool("sub-visibility", true)
-- prevent subtitles from being shown locally
mp.set_property("sub-color", "#00000000")
mp.set_property_number("sub-border-size", 0)

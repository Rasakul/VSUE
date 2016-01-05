package channel.util;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract DTO class for the communication over a socket
 */
public abstract class DataPacket implements Serializable {
	private static final long serialVersionUID = 5950169519310163575L;
	private String       command;
	private List<String> arguments;
	private String       response;
	private boolean      error;
	private String       errorMsg;

	public DataPacket(String command, List<String> arguments) {
		this.command = command;
		this.arguments = arguments;
	}

	public DataPacket(String response) {
		this.response = response;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public boolean hasError() {
		return error;
	}

	public void setError(String errorMsg) {
		this.errorMsg = errorMsg;
		this.error = true;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	@Override
	public int hashCode() {
		int result = command.hashCode();
		result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
		result = 31 * result + (response != null ? response.hashCode() : 0);
		result = 31 * result + (error ? 1 : 0);
		result = 31 * result + (errorMsg != null ? errorMsg.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataPacket that = (DataPacket) o;

		return error == that.error && command.equals(that.command) &&
		       !(arguments != null ? !arguments.equals(that.arguments) : that.arguments != null) &&
		       !(response != null ? !response.equals(that.response) : that.response != null) &&
		       !(errorMsg != null ? !errorMsg.equals(that.errorMsg) : that.errorMsg != null);

	}

	@Override
	public String toString() {
		return "DataPacket{" +
		       "command='" + command + '\'' +
		       ", arguments=" + arguments +
		       ", response='" + response + '\'' +
		       ", error=" + error +
		       ", errorMsg='" + errorMsg + '\'' +
		       '}';
	}

}

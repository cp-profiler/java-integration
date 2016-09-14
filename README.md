The library is available as a Maven package:

```Java
<dependency>
  <groupId>com.github.cp-profiler</groupId>
  <artifactId>cpprof-java</artifactId>
  <version>1.3.0</version>
</dependency>
```


## How to use


Instantiate the Connector class:

```Java
import com.github.cpprofiler.Connector;

Connector connector = new Connector();
```

Connect to a port CP-Profiler would listen to (6565 by default) and start a new tree with `restart` method, specifying a problem's name (optional) and a restart id (`-1` if not a *Restart Beased Search*). In the case of a *Restart Based Search*, the same method should be called  whenever the search restarts.

```Java
// 6565 is the port used by cpprofiler by default
connector.connect(6565);

// starting a new tree (also used in case of a restart)
connector.restart("Problem Name", -1);
```

Create and send a node for each state of the search (i.e. when branching):
```
connector.createNode(id, parent_id, alt, kids, status)
                .setLabel(label)
                .setInfo(info)
                .send();
```

Mandatory fields:

| Field       | Description               |
| :---------: | ----------------------------------- |
| `id`       | Unique identifier for a node |
| `parent_id`       | The identifier of a parent node (`-1` for the root) |
| `alt`       | Which alternative the current node is with respect to its parent (`-1` for the root) |
| `kids`       | Number of children (`2` for binary decisions etc.); `0` if not a decision node |
| `status`       | One of the options provided from `Connector.NodeStatus` in regard to the node's type (`BRANCH`, `SOLVED`, `FAILED` etc.)|

Some optional fields:

| Field       | Description               |
| :---------: | ----------------------------------- |
| `label`       | This text information will be visible on the tree (usually a search decision) |
| `info`       | Arbitrary information in JSON format that can be displayed in a separate text window for each node |

Disconnect when the search is done to release the socket used by *Connector*.
```
connector.disconnect();
```

Check out an example use of this library [here](https://github.com/cp-profiler/java-integration-example).

### More about `info` field

`Info` field should be a JSON-formated string. Most of this information will be ignored by CP-Profiler and simply displayed to the user without any processing. However, the profiler will look for certain fields (e.g. `domains`) and can treat them separately.

To properly specify domains use the following format (given by example):

```
setInfo("{ "domains": {"VarA": "1..10, 12, 14..19", "VarB": "4"} }")
```
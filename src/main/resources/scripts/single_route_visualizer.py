import matplotlib.pyplot as plt
import json
import argparse
import tempfile
import os


def convert_ms_to_datetime(ms):
    # return datetime.datetime.fromtimestamp(ms / 1000.0)
    return ms / 1000


def main(file_path):
    with open(file_path) as routes_by_id_json:
        routes_by_id = json.load(routes_by_id_json)

    script_output = {"route_image_by_id": {}}
    for route_id, route in routes_by_id.items():
        nodes = routes_by_id[route_id]['nodes']
        image_path = draw_route(route_id, nodes)
        script_output["route_image_by_id"].update({route_id : image_path})
    print(json.dumps(script_output))



def draw_route(route_id, nodes):
    activities = [node['node_type'] for node in nodes]
    start_times = [convert_ms_to_datetime(node['service_start_time']) for node in nodes]
    durations = [node["adjusted_service_duration"] / 1000.0 for node in nodes]
    plt.figure(figsize=(10, 6))
    plt.barh(activities, durations, left=start_times, color='skyblue')
    # # add labels
    # for i, (start, duration) in enumerate(zip(start_times, durations)):
    #     plt.text(start + duration / 2, i, activities[i],
    #              ha='center', va='center', color='black')
    temp_dir = tempfile.gettempdir()
    image_path = os.path.join(temp_dir, f"{route_id}.png")
    plt.savefig(image_path)
    plt.close()
    return image_path


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Plot route timeline from a json file")
    parser.add_argument('--file_path', type=str, help='Path to the sample file')

    args = parser.parse_args()

    main(args.file_path)

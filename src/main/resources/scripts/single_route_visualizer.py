import matplotlib.pyplot as plt
import json
import argparse
import tempfile
import os


def convert_ms_to_datetime(ms):
    # return datetime.datetime.fromtimestamp(ms / 1000.0)
    return ms / 1000


def main(route_by_id):
    route_by_id = json.loads(route_by_id)
    nodes = route_by_id['1']['nodes']
    activities = [node['nodeType'] for node in nodes]

    start_times = [convert_ms_to_datetime(node['service_start_time']) for node in nodes]
    durations = [node["adjusted_service_duration"] / 1000.0 for node in nodes]

    plt.figure(figsize=(10, 6))

    plt.barh(activities, durations, left=start_times, color='skyblue')

    # # add labels
    # for i, (start, duration) in enumerate(zip(start_times, durations)):
    #     plt.text(start + duration / 2, i, activities[i],
    #              ha='center', va='center', color='black')

    temp_dir = tempfile.gettempdir()
    image_path = os.path.join(temp_dir, "test.png")
    plt.savefig(image_path)
    plt.close()
    print(image_path)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Plot route timeline from a json file")
    parser.add_argument('--json', type=str, help='route by id json')

    args = parser.parse_args()

    main(args.json)
